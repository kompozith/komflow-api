import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MaterialModule } from 'src/app/material.module';
import { CommonModule } from '@angular/common';
import { StoreManagementFacade } from '../../services/store-management.facade';
import { UpdateStoreRequest, Store } from '../../models/store';
import { BrandManagementFacade } from '../../../brands/services/brand-management.facade';
import { Brand } from '../../../brands/models/brand';
import { combineLatest } from 'rxjs';

@Component({
  selector: 'app-store-edit',
  templateUrl: './store-edit.component.html',
  styleUrls: ['./store-edit.component.scss'],
  imports: [MaterialModule, ReactiveFormsModule, CommonModule],
})
export class StoreEditComponent implements OnInit {
  storeId: string = '';
  isLoading = false;
  storeForm: FormGroup;
  brandOptions: Brand[] = [];
  isLoadingBrands = false;
  store: Store | null = null;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private storeManagementFacade: StoreManagementFacade,
    private brandManagementFacade: BrandManagementFacade,
    private snackBar: MatSnackBar
  ) {
    this.storeForm = new FormGroup({
      name: new FormControl('', [Validators.required, Validators.minLength(2)]),
      description: new FormControl(''),
      phone: new FormControl('', [Validators.required]),
      brandId: new FormControl('', [Validators.required]),
      // Address fields
      streetAddress: new FormControl('', [Validators.required]),
      city: new FormControl('', [Validators.required]),
      region: new FormControl('', [Validators.required]),
      landmark: new FormControl(''),
      instructions: new FormControl(''),
    });
  }

  ngOnInit(): void {
    this.storeId = this.route.snapshot.params['id'];
    if (!this.storeId) {
      this.snackBar.open('Invalid store ID', 'Close', { duration: 3000 });
      this.router.navigate(['../']);
      return;
    }
    this.loadData();
  }

  private loadData(): void {
    this.isLoading = true;

    // Load both store and brands data simultaneously
    combineLatest([
      this.storeManagementFacade.getStoreById(this.storeId),
      this.brandManagementFacade.getBrands({ size: 1000 })
    ]).subscribe({
      next: ([store, brandResponse]) => {
        console.log('Store loaded:', store);
        console.log('Brands loaded:', brandResponse.content);

        this.store = store;
        this.brandOptions = brandResponse.content;
        this.isLoadingBrands = false;

        // Populate form after both data sources are loaded
        this.populateForm(store);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading data:', error);
        this.snackBar.open('Error loading store data', 'Close', { duration: 3000 });
        this.isLoading = false;
        this.isLoadingBrands = false;
        this.router.navigate(['../']);
      }
    });
  }

  private populateForm(store: Store): void {
    console.log('Populating form with store data:', store);
    console.log('Available brands:', this.brandOptions);

    this.storeForm.patchValue({
      name: store.name,
      description: store.description || '',
      phone: store.phone,
      brandId: store.brand?.id || '',
      streetAddress: store.address?.streetAddress || '',
      city: store.address?.city || '',
      region: store.address?.region || '',
      landmark: store.address?.landmark || '',
      instructions: store.address?.instructions || '',
    });

    console.log('Form values after population:', this.storeForm.value);
  }


  getBack(): void {
    this.router.navigate(['../']);
  }

  onSubmit(): void {
    console.log('Form submitted, form valid:', this.storeForm.valid);
    console.log('Form values:', this.storeForm.value);
    console.log('Form errors:', this.getFormErrors());

    if (this.storeForm.valid) {
      const formValue = this.storeForm.value;
      const storeData: UpdateStoreRequest = {
        name: formValue.name.trim(),
        description: formValue.description?.trim() || undefined,
        phone: formValue.phone.trim(),
        brandId: formValue.brandId,
        address: {
          streetAddress: formValue.streetAddress.trim(),
          city: formValue.city.trim(),
          region: formValue.region.trim(),
          postalCode: '',
          country: '',
          landmark: formValue.landmark?.trim() || undefined,
          instructions: formValue.instructions?.trim() || undefined,
        },
      };

      console.log('Sending update request:', storeData);

      this.storeManagementFacade.updateStore(this.storeId, storeData).subscribe({
        next: (store) => {
          console.log('Store updated successfully:', store);
          this.snackBar.open('Store updated successfully', 'Close', { duration: 3000 });
          this.router.navigate(['stores']);
        },
        error: (error) => {
          console.error('Error updating store:', error);
          this.snackBar.open('Error updating store', 'Close', { duration: 3000 });
        }
      });
    } else {
      this.markFormGroupTouched();
      this.snackBar.open('Please fill in all required fields correctly', 'Close', { duration: 3000 });
    }
  }

  private getFormErrors(): any {
    const errors: any = {};
    Object.keys(this.storeForm.controls).forEach(key => {
      const control = this.storeForm.get(key);
      if (control?.errors) {
        errors[key] = control.errors;
      }
    });
    return errors;
  }

  private markFormGroupTouched(): void {
    Object.keys(this.storeForm.controls).forEach(key => {
      const control = this.storeForm.get(key);
      control?.markAsTouched();
    });
  }
}